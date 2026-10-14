package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldLedgerEngine(newRegistry())
        val item = newHoldLedger("code-416", "label-86")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
