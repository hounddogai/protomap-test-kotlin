package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTenderQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTenderQuotaEngine(newRegistry())
        val item = newPrimaryTenderQuota("code-674", "title-807")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
