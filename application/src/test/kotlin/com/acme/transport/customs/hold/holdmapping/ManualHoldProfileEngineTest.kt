package com.acme.transport.customs.hold.holdmapping

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualHoldProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualHoldProfileEngine()
        val item = newManualHoldProfile("code-914", "label-216")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
