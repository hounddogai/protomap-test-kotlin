package com.acme.transport.customs.clearance.seasonalclearanceoverride

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalClearanceOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalClearanceOverrideEngine()
        val item = newGlobalClearanceOverride("code-278", "title-430")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
