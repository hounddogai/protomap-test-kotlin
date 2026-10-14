package com.acme.transport.crewing.qualification.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationLedgerEngine()
        val item = newQualificationLedger("code-784", "name-388")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
