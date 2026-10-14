package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictInspectionProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictInspectionProfileEngine()
        val item = newStrictInspectionProfile("code-112", "name-277")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
