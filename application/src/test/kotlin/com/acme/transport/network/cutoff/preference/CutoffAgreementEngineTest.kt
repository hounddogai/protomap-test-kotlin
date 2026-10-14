package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffAgreementEngine(newRegistry())
        val item = newCutoffAgreement("code-854", "title-697")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
