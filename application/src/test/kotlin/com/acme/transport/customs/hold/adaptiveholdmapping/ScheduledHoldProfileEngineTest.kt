package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledHoldProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledHoldProfileEngine(newRegistry())
        val item = newScheduledHoldProfile("code-991", "name-648")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
