package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDeclarationCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDeclarationCheckpointEngine(newRegistry())
        val item = newLenientDeclarationCheckpoint("code-674", "label-973")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
