package net.minecraftforge.mcpconfig.tasks

import groovy.json.JsonSlurper
import net.minecraftforge.srgutils.IMappingFile
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import static org.objectweb.asm.Opcodes.ACC_STATIC

public class DumpStatics extends SingleFileOutput {
    @InputFile File srg
    @InputFile File meta
    
    @TaskAction
    protected void exec() {
        def srgO = IMappingFile.load(srg)
        def json = new JsonSlurper().parseText(meta.text)
        
        def methods = [] as HashSet

        json.each{ cls,data ->
            data['methods']?.findAll{k,v -> (v['access'] & ACC_STATIC) != 0}.each{ __, method ->
                def name = method['name']
                def desc = method['desc']
                def mapped = srgO.getClass(cls)?.remapMethod(name, desc)
                if (mapped != null && !desc.contains('()') && mapped.contains('func_'))
                    methods.add(mapped)
            }
        }

        dest.withWriter('UTF-8') { writer ->
            methods = methods.sort{it}
            methods.each{ writer.write(it + '\n') }
        }
    }
}
