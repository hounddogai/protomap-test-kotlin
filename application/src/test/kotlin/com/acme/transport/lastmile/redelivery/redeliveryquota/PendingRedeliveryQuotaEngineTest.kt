package com.acme.transport.lastmile.redelivery.redeliveryquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRedeliveryQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRedeliveryQuotaEngine(newRegistry())
        val item = newPendingRedeliveryQuota("code-63", "title-548")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
