/*
 * Copyright 2011 Martin Hauner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugin.cucumber

import org.jruby.embed.ScriptingContainer
import org.jruby.embed.EvalFailedException
import org.jruby.embed.LocalContextScope


class JRubyGem {
    final String JGEM_RESOURCE_PATH = '/META-INF/jruby.home/bin/jgem'

    void run (args) {
        // run as thread, otherwise we can not control the environment
        // on subsequent jruby calls.
        def thread = Thread.start {
            println "** THREAD START"
            try {
                runScript (args)
            }
            // ruby code calling "exit n" will throw, so we catch it.
            catch (EvalFailedException e) {
                println "jruby says: ${e.getMessage ()}"
            }
            finally {
                println "jruby finally"
            }
            println "** THREAD END"
        }
        thread.join ()

        println "** THREAD JOINED"
    }


    def runScript (args) {
        println "*** CLASSPATH >>"
        for (String property: System.getProperty ("java.class.path").split (":")) {
            println property
        }
        println "*** CLASSPATH <<"

        def jruby = new ScriptingContainer (LocalContextScope.THREADSAFE)
        jruby.setArgv (args as String[])

        Reader reader = jgemReader ()

        println "*** JRUBY run scriptlet"

        //jruby.put('$GROOVY', 12345);
        //def result = jruby.runScriptlet('puts $GROOVY.to_s(2)')

        def result = jruby.runScriptlet (reader, "jgem")

        println "*** JRUBY result: ${result}"
    }

    Reader jgemReader () {
        InputStream stream = getClass().getResourceAsStream (JGEM_RESOURCE_PATH)

        // todo: clean up, throw error if not found
        if (stream == null) {
            println "** did not find jgem"
        }
        else {
            println "** found jgem"
        }

        new InputStreamReader (stream)
    }
}
