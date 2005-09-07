package org.apache.maven.artifact.repository.metadata;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Metadata for the artifact version directory of the repository.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @todo split instantiation (versioning, plugin mappings) from definition
 */
public class SnapshotArtifactRepositoryMetadata
    extends AbstractRepositoryMetadata
{
    private Snapshot snapshot;

    private Artifact artifact;

    public SnapshotArtifactRepositoryMetadata( Artifact artifact )
    {
        this.artifact = artifact;
    }

    public SnapshotArtifactRepositoryMetadata( Artifact artifact, Snapshot snapshot )
    {
        this.snapshot = snapshot;
        this.artifact = artifact;
    }

    public String toString()
    {
        return "repository metadata for: \'" + getKey() + "\'";
    }

    public boolean storedInGroupDirectory()
    {
        return false;
    }

    public boolean storedInArtifactVersionDirectory()
    {
        return true;
    }

    public String getGroupId()
    {
        return artifact.getGroupId();
    }

    public String getArtifactId()
    {
        return artifact.getArtifactId();
    }

    public String getBaseVersion()
    {
        return artifact.getBaseVersion();
    }

    protected void updateRepositoryMetadata( ArtifactRepository localRepository, ArtifactRepository remoteRepository )
        throws IOException
    {
        MetadataXpp3Reader mappingReader = new MetadataXpp3Reader();

        Metadata metadata = null;

        File metadataFile = new File( localRepository.getBasedir(),
                                      localRepository.pathOfLocalRepositoryMetadata( this, remoteRepository ) );

        if ( metadataFile.exists() )
        {
            Reader reader = null;

            try
            {
                reader = new FileReader( metadataFile );

                metadata = mappingReader.read( reader );
            }
            catch ( FileNotFoundException e )
            {
                // TODO: Log a warning
            }
            catch ( IOException e )
            {
                // TODO: Log a warning
            }
            catch ( XmlPullParserException e )
            {
                // TODO: Log a warning
            }
            finally
            {
                IOUtil.close( reader );
            }
        }

        boolean changed = false;

        // If file could not be found or was not valid, start from scratch
        if ( metadata == null )
        {
            metadata = new Metadata();

            metadata.setGroupId( artifact.getGroupId() );
            metadata.setArtifactId( artifact.getArtifactId() );
            changed = true;
        }

        if ( snapshot != null )
        {
            Versioning v = metadata.getVersioning();
            if ( v == null )
            {
                v = new Versioning();
                metadata.setVersioning( v );
            }

            Snapshot s = v.getSnapshot();
            if ( s == null )
            {
                v.setSnapshot( snapshot );
                changed = true;
            }
            else if ( s.getTimestamp() != null && !s.getTimestamp().equals( snapshot.getTimestamp() ) )
            {
                s.setTimestamp( snapshot.getTimestamp() );
                changed = true;
            }
            else if ( s.getBuildNumber() != snapshot.getBuildNumber() )
            {
                s.setBuildNumber( snapshot.getBuildNumber() );
                changed = true;
            }
        }

        if ( changed )
        {
            Writer writer = null;
            try
            {
                metadataFile.getParentFile().mkdirs();
                writer = new FileWriter( metadataFile );

                MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();

                mappingWriter.write( writer, metadata );
            }
            finally
            {
                IOUtil.close( writer );
            }
        }
        else
        {
            metadataFile.setLastModified( System.currentTimeMillis() );
        }
    }

    public Object getKey()
    {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getBaseVersion();
    }

    public boolean isSnapshot()
    {
        return artifact.isSnapshot();
    }

}