package org.hackerandpainter.databasedocgenerator.core;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.service.*;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2MapperImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static springfox.documentation.builders.BuilderDefaults.nullToEmptyList;

@Component
@Primary
@Slf4j
public class SortedParameter extends ServiceModelToSwagger2MapperImpl {
    @Override
    public Swagger mapDocumentation(Documentation from) {
        Swagger swagger = super.mapDocumentation(from);
        Multimap<String, ApiListing> apiListings = from.getApiListings();
        swagger.setPaths(sortedParameter(apiListings));
        return swagger;
    }

    private Map<String, Path> sortedParameter(Multimap<String, ApiListing> apiListings) {
        log.info("*******  sorted parameter...  ");
        Iterator iter = apiListings.entries().iterator();
        Map<String, Path> newPath = new LinkedHashMap<>();
        while (iter.hasNext()) {
            Map.Entry<String, ApiListing> entry = (Map.Entry<String, ApiListing>) iter.next();
            ApiListing value = entry.getValue();
            List<ApiDescription> apis = value.getApis();
            for (ApiDescription api : apis) {
                List<Operation> operations = api.getOperations();
                for (Operation operation : operations) {
                    List<Parameter> parameters = operation.getParameters();
                    List<Parameter> parameterList = sortedParameterCore(parameters);
                    modify(operation,"parameters",parameterList);
                }
                newPath.put(api.getPath(),  mapOperations(api, Optional.fromNullable(newPath.get(api.getPath()))));
            }
        }
        return newPath;
    }

    private List<Parameter> sortedParameterCore(List<Parameter> parameters) {
        return parameters.stream()
                .sorted(Comparator.comparing(Parameter::getDescription))
                .collect(Collectors.toList());
    }

    public static void modify(Object object, String fieldName, Object newFieldValue) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true); //Field 的 modifiers 是私有的
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            if(!field.isAccessible()) {
                field.setAccessible(true);
            }

            field.set(object, newFieldValue);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Path mapOperations(ApiDescription api, Optional<Path> existingPath) {
        Path path = existingPath.or(new Path());
        for (Operation each : nullToEmptyList(api.getOperations())) {
            io.swagger.models.Operation operation = mapOperation(each);
            path.set(each.getMethod().toString().toLowerCase(), operation);
        }
        return path;
    }
}
