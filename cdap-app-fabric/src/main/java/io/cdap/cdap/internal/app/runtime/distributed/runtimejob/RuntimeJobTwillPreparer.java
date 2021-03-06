/*
 * Copyright © 2020 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.cdap.internal.app.runtime.distributed.runtimejob;

import io.cdap.cdap.app.runtime.ProgramOptions;
import io.cdap.cdap.common.conf.CConfiguration;
import io.cdap.cdap.internal.app.runtime.distributed.AbstractRuntimeTwillPreparer;
import io.cdap.cdap.internal.app.runtime.distributed.runtime.TwillControllerFactory;
import io.cdap.cdap.proto.id.ProgramRunId;
import io.cdap.cdap.runtime.spi.runtimejob.RuntimeJobManager;
import org.apache.hadoop.conf.Configuration;
import org.apache.twill.api.LocalFile;
import org.apache.twill.api.RuntimeSpecification;
import org.apache.twill.api.TwillPreparer;
import org.apache.twill.api.TwillRunnable;
import org.apache.twill.api.TwillSpecification;
import org.apache.twill.filesystem.LocationFactory;
import org.apache.twill.internal.JvmOptions;
import org.apache.twill.internal.TwillRuntimeSpecification;
import org.apache.twill.internal.io.LocationCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  A {@link TwillPreparer} implementation that uses runtime job manager to launch a single {@link TwillRunnable}.
 */
class RuntimeJobTwillPreparer extends AbstractRuntimeTwillPreparer {
  private static final Logger LOG = LoggerFactory.getLogger(RuntimeJobTwillPreparer.class);

  private final RuntimeJobManager jobManager;

  RuntimeJobTwillPreparer(CConfiguration cConf, Configuration hConf,
                          TwillSpecification twillSpec, ProgramRunId programRunId,
                          ProgramOptions programOptions, LocationCache locationCache,
                          LocationFactory locationFactory, TwillControllerFactory controllerFactory,
                          RuntimeJobManager jobManager) {
    super(cConf, hConf, twillSpec, programRunId, programOptions, locationCache, locationFactory, controllerFactory);
    this.jobManager = jobManager;
  }

  @Override
  protected void launch(TwillRuntimeSpecification twillRuntimeSpec, RuntimeSpecification runtimeSpec,
                        JvmOptions jvmOptions, Map<String, String> environments, Map<String, LocalFile> localFiles,
                        TimeoutChecker timeoutChecker) throws Exception {
    jobManager.initialize();
    timeoutChecker.throwIfTimeout();
    DefaultRuntimeInfo defaultRuntimeInfo = createRuntimeJobInfo(runtimeSpec, localFiles);
    LOG.info("Starting runnable {} for runId {} with job manager.", runtimeSpec.getName(), getProgramRunId());
    // launch job using job manager
    jobManager.launch(defaultRuntimeInfo);
  }

  private DefaultRuntimeInfo createRuntimeJobInfo(RuntimeSpecification runtimeSpec, Map<String, LocalFile> localFiles) {
    return new DefaultRuntimeInfo(getProgramRunId(),
                                  Stream.concat(localFiles.values().stream(), runtimeSpec.getLocalFiles().stream())
                                    .collect(Collectors.toList()));
  }
}
