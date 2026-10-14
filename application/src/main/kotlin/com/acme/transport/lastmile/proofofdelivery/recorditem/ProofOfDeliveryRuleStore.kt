package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery rules by code. */
interface ProofOfDeliveryRuleStore {
    fun find(code: String): ProofOfDeliveryRule?
    fun save(item: ProofOfDeliveryRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryRuleStore(
    var items: MutableMap<String, ProofOfDeliveryRule>,
    var order: MutableList<String>,
) : ProofOfDeliveryRuleStore {
    override fun find(code: String): ProofOfDeliveryRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryRule) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<ProofOfDeliveryRule> {
        val out = mutableListOf<ProofOfDeliveryRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryRuleStore(): ProofOfDeliveryRuleStore {
    return MemoryProofOfDeliveryRuleStore(mutableMapOf<String, ProofOfDeliveryRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryRuleStore(
    var inner: ProofOfDeliveryRuleStore,
    var cache: TtlCache<ProofOfDeliveryRule>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryRuleStore {
    override fun find(code: String): ProofOfDeliveryRule? {
        val hit = (cache.get(code) as ProofOfDeliveryRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryRule> = inner.list()
}

fun newCachedProofOfDeliveryRuleStore(inner: ProofOfDeliveryRuleStore, ttl: Int, clock: Clock): ProofOfDeliveryRuleStore {
    return CachedProofOfDeliveryRuleStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery rules. Empty fields match all. */
data class ProofOfDeliveryRuleQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryRule>): MutableList<ProofOfDeliveryRule> {
        val matched = filter(items) { candidate: ProofOfDeliveryRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryRules(store: ProofOfDeliveryRuleStore, query: ProofOfDeliveryRuleQuery): MutableList<ProofOfDeliveryRule> {
    return query.apply(store.list())
}
