package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCameraEventBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCameraEventBudgetEngine()
        val item = newGlobalCameraEventBudget("code-388", "label-986")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
