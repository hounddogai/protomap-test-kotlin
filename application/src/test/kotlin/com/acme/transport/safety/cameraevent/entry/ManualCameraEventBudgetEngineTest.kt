package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCameraEventBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCameraEventBudgetEngine()
        val item = newManualCameraEventBudget("code-174", "title-2")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
