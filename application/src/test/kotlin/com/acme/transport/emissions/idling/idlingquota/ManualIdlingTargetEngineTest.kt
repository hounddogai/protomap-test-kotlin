package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualIdlingTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualIdlingTargetEngine(newRegistry())
        val item = newManualIdlingTarget("code-695", "title-529")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
