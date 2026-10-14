package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryQuotaEngine(newRegistry())
        val item = newRedeliveryQuota("code-580", "label-228")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
