package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultAccessorialCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultAccessorialCycleEngine(newRegistry())
        val item = newDefaultAccessorialCycle("code-693", "name-541")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
