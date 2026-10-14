package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientInspectionWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientInspectionWindowEngine(newRegistry())
        val item = newLenientInspectionWindow("code-69", "label-25")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
