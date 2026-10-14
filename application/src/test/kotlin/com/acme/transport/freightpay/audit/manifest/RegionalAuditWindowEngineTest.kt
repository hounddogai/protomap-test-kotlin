package com.acme.transport.freightpay.audit.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalAuditWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalAuditWindowEngine()
        val item = newRegionalAuditWindow("code-685", "name-684")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
