package com.acme.transport.customs.declaration.review

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration reviews by code. */
interface DeclarationReviewStore {
    fun find(code: String): DeclarationReview?
    fun save(item: DeclarationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationReviewStore(
    var items: MutableMap<String, DeclarationReview>,
    var order: MutableList<String>,
) : DeclarationReviewStore {
    override fun find(code: String): DeclarationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationReview) {
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

    override fun list(): MutableList<DeclarationReview> {
        val out = mutableListOf<DeclarationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationReviewStore(): DeclarationReviewStore {
    return MemoryDeclarationReviewStore(mutableMapOf<String, DeclarationReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationReviewStore(
    var inner: DeclarationReviewStore,
    var cache: TtlCache<DeclarationReview>,
    var hits: Int,
    var misses: Int,
) : DeclarationReviewStore {
    override fun find(code: String): DeclarationReview? {
        val hit = (cache.get(code) as DeclarationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationReview> = inner.list()
}

fun newCachedDeclarationReviewStore(inner: DeclarationReviewStore, ttl: Int, clock: Clock): DeclarationReviewStore {
    return CachedDeclarationReviewStore(
        inner = inner,
        cache = newTtlCache<DeclarationReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration reviews. Empty fields match all. */
data class DeclarationReviewQuery(
    var locale: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationReview): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationReview>): MutableList<DeclarationReview> {
        val matched = filter(items) { candidate: DeclarationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationReviews(store: DeclarationReviewStore, query: DeclarationReviewQuery): MutableList<DeclarationReview> {
    return query.apply(store.list())
}
