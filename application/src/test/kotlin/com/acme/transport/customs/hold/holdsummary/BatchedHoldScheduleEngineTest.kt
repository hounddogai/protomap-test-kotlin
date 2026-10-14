package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedHoldScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedHoldScheduleEngine(newRegistry())
        val item = newBatchedHoldSchedule("code-889", "label-491")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
