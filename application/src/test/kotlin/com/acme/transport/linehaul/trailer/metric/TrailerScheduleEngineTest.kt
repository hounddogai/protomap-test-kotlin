package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerScheduleEngine(newRegistry())
        val item = newTrailerSchedule("code-483", "name-776")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
