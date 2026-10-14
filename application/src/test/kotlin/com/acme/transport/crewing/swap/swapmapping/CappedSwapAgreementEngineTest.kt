package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedSwapAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedSwapAgreementEngine(newRegistry())
        val item = newCappedSwapAgreement("code-443", "name-340")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
