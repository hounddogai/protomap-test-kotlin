package com.acme.transport.lastmile.locker.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualLockerPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualLockerPreferenceEngine()
        val item = newManualLockerPreference("code-361", "title-75")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
