package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRateCardNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRateCardNoticeEngine(newRegistry())
        val item = newCappedRateCardNotice("code-240", "title-732")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
