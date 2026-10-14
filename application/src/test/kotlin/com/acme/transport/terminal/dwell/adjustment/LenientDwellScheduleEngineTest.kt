package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDwellScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDwellScheduleEngine(newRegistry())
        val item = newLenientDwellSchedule("code-38", "label-673")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
