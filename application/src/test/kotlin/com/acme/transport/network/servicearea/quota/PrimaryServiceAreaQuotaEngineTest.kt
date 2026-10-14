package com.acme.transport.network.servicearea.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryServiceAreaQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryServiceAreaQuotaEngine(newRegistry())
        val item = newPrimaryServiceAreaQuota("code-641", "label-118")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
