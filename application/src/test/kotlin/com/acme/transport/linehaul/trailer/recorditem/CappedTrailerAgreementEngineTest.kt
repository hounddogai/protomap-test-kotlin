package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTrailerAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTrailerAgreementEngine(newRegistry())
        val item = newCappedTrailerAgreement("code-64", "title-164")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
