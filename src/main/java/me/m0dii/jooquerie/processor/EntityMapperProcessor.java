package me.m0dii.jooquerie.processor;

import me.m0dii.jooquerie.dsl.annotation.Entity;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes("me.m0dii.jooquerie.dsl.annotation.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class EntityMapperProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                try {
                    generateMapperFor(typeElement);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "Failed to generate mapper for " + typeElement.getQualifiedName() + ": " + e.getMessage(),
                            element
                    );
                }
            }
        }
        return true;
    }

    private void generateMapperFor(TypeElement typeElement) throws IOException {
        // TODO: Implement the logic to generate a mapper class for the entity
    }
}
