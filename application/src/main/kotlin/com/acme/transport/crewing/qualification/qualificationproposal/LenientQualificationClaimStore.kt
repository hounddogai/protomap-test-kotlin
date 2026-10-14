package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient qualification claims by code. */
interface LenientQualificationClaimStore {
    fun find(code: String): LenientQualificationClaim?
    fun save(item: LenientQualificationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientQualificationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientQualificationClaimStore(
    var items: MutableMap<String, LenientQualificationClaim>,
    var order: MutableList<String>,
) : LenientQualificationClaimStore {
    override fun find(code: String): LenientQualificationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientQualificationClaim) {
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

    override fun list(): MutableList<LenientQualificationClaim> {
        val out = mutableListOf<LenientQualificationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientQualificationClaimStore(): LenientQualificationClaimStore {
    return MemoryLenientQualificationClaimStore(
        mutableMapOf<String, LenientQualificationClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientQualificationClaimStore(
    var inner: LenientQualificationClaimStore,
    var cache: TtlCache<LenientQualificationClaim>,
    var hits: Int,
    var misses: Int,
) : LenientQualificationClaimStore {
    override fun find(code: String): LenientQualificationClaim? {
        val hit = (cache.get(code) as LenientQualificationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientQualificationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientQualificationClaim> = inner.list()
}

fun newCachedLenientQualificationClaimStore(inner: LenientQualificationClaimStore, ttl: Int, clock: Clock): LenientQualificationClaimStore {
    return CachedLenientQualificationClaimStore(
        inner = inner,
        cache = newTtlCache<LenientQualificationClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient qualification claims. Empty fields match all. */
data class LenientQualificationClaimQuery(
    var channel: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientQualificationClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<LenientQualificationClaim>): MutableList<LenientQualificationClaim> {
        val matched = filter(items) { candidate: LenientQualificationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientQualificationClaims(store: LenientQualificationClaimStore, query: LenientQualificationClaimQuery): MutableList<LenientQualificationClaim> {
    return query.apply(store.list())
}
