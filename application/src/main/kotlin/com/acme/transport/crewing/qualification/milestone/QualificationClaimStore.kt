package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification claims by code. */
interface QualificationClaimStore {
    fun find(code: String): QualificationClaim?
    fun save(item: QualificationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationClaimStore(
    var items: MutableMap<String, QualificationClaim>,
    var order: MutableList<String>,
) : QualificationClaimStore {
    override fun find(code: String): QualificationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationClaim) {
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

    override fun list(): MutableList<QualificationClaim> {
        val out = mutableListOf<QualificationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationClaimStore(): QualificationClaimStore {
    return MemoryQualificationClaimStore(mutableMapOf<String, QualificationClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationClaimStore(
    var inner: QualificationClaimStore,
    var cache: TtlCache<QualificationClaim>,
    var hits: Int,
    var misses: Int,
) : QualificationClaimStore {
    override fun find(code: String): QualificationClaim? {
        val hit = (cache.get(code) as QualificationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationClaim> = inner.list()
}

fun newCachedQualificationClaimStore(inner: QualificationClaimStore, ttl: Int, clock: Clock): QualificationClaimStore {
    return CachedQualificationClaimStore(
        inner = inner,
        cache = newTtlCache<QualificationClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification claims. Empty fields match all. */
data class QualificationClaimQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationClaim>): MutableList<QualificationClaim> {
        val matched = filter(items) { candidate: QualificationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationClaims(store: QualificationClaimStore, query: QualificationClaimQuery): MutableList<QualificationClaim> {
    return query.apply(store.list())
}
