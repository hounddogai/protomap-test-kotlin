package com.acme.transport.customs.broker.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingBrokerQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingBrokerQuotaEngine(newRegistry())
        val item = newRollingBrokerQuota("code-119", "label-910")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
