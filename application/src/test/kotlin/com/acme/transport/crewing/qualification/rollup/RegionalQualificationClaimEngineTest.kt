package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalQualificationClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalQualificationClaimEngine(newRegistry())
        val item = newRegionalQualificationClaim("code-318", "name-296")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
