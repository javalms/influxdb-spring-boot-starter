package com.github.gxhunter.influx.config;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.io.IOException;

public class InterfaceTypeFilter extends AssignableTypeFilter{

    /**
     * Creates a new {@link InterfaceTypeFilter}.
     *
     * @param targetType
     */
    public InterfaceTypeFilter(Class<?> targetType) {
        super(targetType);
    }

    /**
     * (non-Javadoc)
     * @see org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter#match(MetadataReader, MetadataReaderFactory)
     */
    @Override
    public boolean match(MetadataReader metadataReader,MetadataReaderFactory metadataReaderFactory)
            throws IOException{

        return metadataReader.getClassMetadata().isInterface() && super.match(metadataReader, metadataReaderFactory);
    }
}
