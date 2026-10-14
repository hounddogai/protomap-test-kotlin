package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryCameraEventTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryCameraEventTargetEngine(newRegistry())
        val item = newPrimaryCameraEventTarget("code-11", "title-96")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
