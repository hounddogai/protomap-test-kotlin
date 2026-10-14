package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul agreements by code. */
interface BackhaulAgreementStore {
    fun find(code: String): BackhaulAgreement?
    fun save(item: BackhaulAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulAgreementStore(
    var items: MutableMap<String, BackhaulAgreement>,
    var order: MutableList<String>,
) : BackhaulAgreementStore {
    override fun find(code: String): BackhaulAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulAgreement) {
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

    override fun list(): MutableList<BackhaulAgreement> {
        val out = mutableListOf<BackhaulAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulAgreementStore(): BackhaulAgreementStore {
    return MemoryBackhaulAgreementStore(mutableMapOf<String, BackhaulAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulAgreementStore(
    var inner: BackhaulAgreementStore,
    var cache: TtlCache<BackhaulAgreement>,
    var hits: Int,
    var misses: Int,
) : BackhaulAgreementStore {
    override fun find(code: String): BackhaulAgreement? {
        val hit = (cache.get(code) as BackhaulAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulAgreement> = inner.list()
}

fun newCachedBackhaulAgreementStore(inner: BackhaulAgreementStore, ttl: Int, clock: Clock): BackhaulAgreementStore {
    return CachedBackhaulAgreementStore(
        inner = inner,
        cache = newTtlCache<BackhaulAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul agreements. Empty fields match all. */
data class BackhaulAgreementQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulAgreement>): MutableList<BackhaulAgreement> {
        val matched = filter(items) { candidate: BackhaulAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulAgreements(store: BackhaulAgreementStore, query: BackhaulAgreementQuery): MutableList<BackhaulAgreement> {
    return query.apply(store.list())
}
