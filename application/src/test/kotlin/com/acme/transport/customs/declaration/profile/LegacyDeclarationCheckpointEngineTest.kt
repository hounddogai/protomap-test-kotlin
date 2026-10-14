package com.acme.transport.customs.declaration.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDeclarationCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDeclarationCheckpointEngine()
        val item = newLegacyDeclarationCheckpoint("code-540", "name-493")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
