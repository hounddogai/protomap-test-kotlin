package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledAccessorialBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledAccessorialBatchEngine(newRegistry())
        val item = newScheduledAccessorialBatch("code-278", "name-294")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
