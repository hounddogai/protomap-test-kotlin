package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderClaimEngine(newRegistry())
        val item = newTenderClaim("code-48", "name-63")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
