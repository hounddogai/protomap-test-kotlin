package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTrailerYardAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTrailerYardAgreementEngine(newRegistry())
        val item = newManualTrailerYardAgreement("code-196", "name-197")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
