package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient proof of delivery rules by code. */
interface LenientProofOfDeliveryRuleStore {
    fun find(code: String): LenientProofOfDeliveryRule?
    fun save(item: LenientProofOfDeliveryRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientProofOfDeliveryRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientProofOfDeliveryRuleStore(
    var items: MutableMap<String, LenientProofOfDeliveryRule>,
    var order: MutableList<String>,
) : LenientProofOfDeliveryRuleStore {
    override fun find(code: String): LenientProofOfDeliveryRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientProofOfDeliveryRule) {
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

    override fun list(): MutableList<LenientProofOfDeliveryRule> {
        val out = mutableListOf<LenientProofOfDeliveryRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientProofOfDeliveryRuleStore(): LenientProofOfDeliveryRuleStore {
    return MemoryLenientProofOfDeliveryRuleStore(
        mutableMapOf<String, LenientProofOfDeliveryRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientProofOfDeliveryRuleStore(
    var inner: LenientProofOfDeliveryRuleStore,
    var cache: TtlCache<LenientProofOfDeliveryRule>,
    var hits: Int,
    var misses: Int,
) : LenientProofOfDeliveryRuleStore {
    override fun find(code: String): LenientProofOfDeliveryRule? {
        val hit = (cache.get(code) as LenientProofOfDeliveryRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientProofOfDeliveryRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientProofOfDeliveryRule> = inner.list()
}

fun newCachedLenientProofOfDeliveryRuleStore(inner: LenientProofOfDeliveryRuleStore, ttl: Int, clock: Clock): LenientProofOfDeliveryRuleStore {
    return CachedLenientProofOfDeliveryRuleStore(
        inner = inner,
        cache = newTtlCache<LenientProofOfDeliveryRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient proof of delivery rules. Empty fields match all. */
data class LenientProofOfDeliveryRuleQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientProofOfDeliveryRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientProofOfDeliveryRule>): MutableList<LenientProofOfDeliveryRule> {
        val matched = filter(items) { candidate: LenientProofOfDeliveryRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientProofOfDeliveryRules(store: LenientProofOfDeliveryRuleStore, query: LenientProofOfDeliveryRuleQuery): MutableList<LenientProofOfDeliveryRule> {
    return query.apply(store.list())
}
