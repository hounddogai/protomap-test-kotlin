package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakAgreementEngine(newRegistry())
        val item = newRestBreakAgreement("code-402", "title-85")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
