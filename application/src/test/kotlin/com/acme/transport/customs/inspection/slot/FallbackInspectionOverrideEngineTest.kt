package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackInspectionOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackInspectionOverrideEngine(newRegistry())
        val item = newFallbackInspectionOverride("code-672", "title-588")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
