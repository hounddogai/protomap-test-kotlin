package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTenderClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTenderClaimEngine(newRegistry())
        val item = newCappedTenderClaim("code-308", "name-936")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
