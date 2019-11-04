package com.github.gxhunter.influx.config;

import com.github.gxhunter.influx.BaseInfluxMapper;
import com.github.gxhunter.influx.InfluxDBFactoryBean;
import com.github.gxhunter.influx.annotation.InfluxMapperScan;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author hunter
 */
public class InfluxClientRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware{
    private Environment environment;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata,
                                        BeanDefinitionRegistry registry){

        try{
            ClassPathScanningCandidateComponentProvider scanner = getScanner();

            Set<String> basePackages = getBasePackages(metadata);
            Set<BeanDefinition> candidateComponents = new HashSet<>();
            basePackages.forEach(basePackage -> {
                candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
            });

            for(BeanDefinition beanDefinition : candidateComponents){
                //            泛型类型
                Class entityType = null;
                Type[] genericInterfaces = Class.forName(beanDefinition.getBeanClassName()).getGenericInterfaces();
                for(Type type : genericInterfaces){
                    if(type instanceof ParameterizedTypeImpl){
                        ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) type;
                        if(parameterizedType.getRawType() == BaseInfluxMapper.class){
                            entityType = Class.forName(parameterizedType.getActualTypeArguments()[0].getTypeName());
                        }
                    }
                }
                AbstractBeanDefinition rawBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(InfluxDBFactoryBean.class)
                        .addPropertyValue("beanClassName",beanDefinition.getBeanClassName())
                        .addPropertyValue("entityClass",entityType)
                        .getBeanDefinition();
                rawBeanDefinition.setPrimary(true);
                registry.registerBeanDefinition(beanDefinition.getBeanClassName(),rawBeanDefinition);
            }
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }


    }

    /**
     * @param importingClassMetadata
     * @return
     */
    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata){
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(InfluxMapperScan.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        if(CollectionUtils.isEmpty(attributes)){
            return basePackages;
        }
        for(String pkg : (String[]) attributes.get("basePackages")){
            if(StringUtils.hasText(pkg)){
                basePackages.add(pkg);
            }
        }

        if(basePackages.isEmpty()){
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

    /**
     * 获取扫描器
     * @return
     */
    protected ClassPathScanningCandidateComponentProvider getScanner(){
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false,this.environment){
            @Override
            protected boolean isCandidateComponent(
                    AnnotatedBeanDefinition beanDefinition){
                boolean isCandidate = false;
                if(beanDefinition.getMetadata().isIndependent()){
                    if(!beanDefinition.getMetadata().isAnnotation()){
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
        scanner.setEnvironment(environment);
        scanner.addIncludeFilter(new InterfaceTypeFilter(BaseInfluxMapper.class));
        return scanner;
    }

    @Override
    public void setEnvironment(Environment environment){
        this.environment = environment;
    }


}
