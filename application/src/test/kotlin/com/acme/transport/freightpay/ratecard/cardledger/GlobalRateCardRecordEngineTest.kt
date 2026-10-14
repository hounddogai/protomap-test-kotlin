package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRateCardRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRateCardRecordEngine(newRegistry())
        val item = newGlobalRateCardRecord("code-945", "label-618")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
