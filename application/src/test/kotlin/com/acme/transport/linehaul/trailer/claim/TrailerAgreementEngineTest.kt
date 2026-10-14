package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerAgreementEngine(newRegistry())
        val item = newTrailerAgreement("code-559", "name-990")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
