package dev.expo.macaw.tbainterface

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.JsonObject
import com.sun.codemodel.JCodeModel
import org.jsonschema2pojo.*
import org.jsonschema2pojo.rules.RuleFactory
import java.io.File
import java.lang.reflect.ParameterizedType


object JsonInterface {
    private val mapper: SchemaMapper
    private val model: JCodeModel
    private val config: GenerationConfig


    fun generateClass(key: String, className: String) {
        val source = TBA.getJsonString(key)

        mapper.generate(model, className, "dev.expo.macaw.tbadata.$className", source)

        val destination = File("src/main/java")
        destination.mkdirs()
        model.build(destination)
    }

    fun fill(key: String, target: Any) {
        val data = TBA.getJson(key).asJsonObject
        populate(data, target)
    }

    fun populate(data: JsonObject, target: Any) {
        for (field in target.javaClass.fields) {
            val jsonFieldName = field.getAnnotation(JsonProperty::class.java).value
            val jsonField = data.get(jsonFieldName)
            when {
                jsonField.isJsonPrimitive -> {
                    when {
                        jsonField.asJsonPrimitive.isNumber -> {
                            val num = data.get(jsonFieldName)
                            field.set(target, Integer.valueOf(num.asInt))
                        }

                        jsonField.asJsonPrimitive.isString -> {
                            val str = data.get(jsonFieldName).asString
                            field.set(target, str)
                        }

                        jsonField.asJsonPrimitive.isBoolean -> {
                            val bool = data.get(jsonFieldName).asBoolean
                            field.set(target, bool)
                        }
                    }
                }

                jsonField.isJsonObject -> {
                    val instance = field.type.getDeclaredConstructor().newInstance()
                    populate(data.get(jsonFieldName).asJsonObject, instance)
                    field.set(target, instance)
                }

                jsonField.isJsonNull -> {
                    field.set(target, null)
                }

                jsonField.isJsonArray -> {
                    val ls = data.get(jsonFieldName)

                    val outputList = mutableListOf<Any>()

                    for (d in ls.asJsonArray) {
                        if (d.isJsonObject) {
                            val listTypeClass = Class.forName(
                                (field.genericType as ParameterizedType).actualTypeArguments[0].toString().substring(6)
                            )
                            if (listTypeClass.isAnnotationPresent(JsonInclude::class.java)) {
                                val instance = listTypeClass.getDeclaredConstructor().newInstance()

                                populate(d.asJsonObject, instance)
                                outputList.add(instance)
                            }
                        } else {
                            when {
                                d.asJsonPrimitive.isBoolean -> outputList.add(d.asJsonPrimitive.asBoolean)
                                d.asJsonPrimitive.isNumber -> outputList.add(d.asJsonPrimitive.asInt)
                                d.asJsonPrimitive.isString -> outputList.add(d.asJsonPrimitive.asString)
                            }
                        }
                    }

                    field.set(target, outputList.toList())
                }


            }
        }
    }

    init {
        config = object : DefaultGenerationConfig() {
            override fun isGenerateBuilders(): Boolean {
                return false
            }

            override fun isUsePrimitives(): Boolean {
                return false
            }

            override fun isIncludeGetters(): Boolean {
                return false
            }

            override fun getAnnotationStyle(): AnnotationStyle {
                return AnnotationStyle.NONE
            }

            override fun getSourceType(): SourceType {
                return SourceType.JSON
            }

            override fun isIncludeSetters(): Boolean {
                return false
            }

            override fun isIncludeToString(): Boolean {
                return false
            }

            override fun isIncludeAdditionalProperties(): Boolean {
                return true
            }

            override fun isIncludeHashcodeAndEquals(): Boolean {
                return false
            }

            override fun isIncludeConstructorPropertiesAnnotation(): Boolean {
                return false
            }

            override fun isIncludeJsr305Annotations(): Boolean {
                return false
            }

            override fun isIncludeGeneratedAnnotation(): Boolean {
                return true
            }

            override fun isIncludeJsr303Annotations(): Boolean {
                return false
            }

            override fun isInitializeCollections(): Boolean {
                return false
            }
        }
        mapper = SchemaMapper(RuleFactory(config, Jackson2Annotator(config), SchemaStore()), SchemaGenerator())
        model = JCodeModel()
    }

}