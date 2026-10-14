package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationCheckpointEngine(newRegistry())
        val item = newDeclarationCheckpoint("code-697", "title-488")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
