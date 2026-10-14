package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderAgreementEngine(newRegistry())
        val item = newTenderAgreement("code-456", "title-2")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
