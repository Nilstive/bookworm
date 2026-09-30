-- V1__init_schema.sql
-- Book Worm initial database schema

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Users
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    gift_points   INT NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

-- Authors
CREATE TABLE authors (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    bio        TEXT,
    photo_url  VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Books
CREATE TABLE books (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title             VARCHAR(500) NOT NULL,
    author_id         UUID NOT NULL REFERENCES authors(id),
    cover_image_url   VARCHAR(500),
    format            VARCHAR(20) NOT NULL CHECK (format IN ('PAPERBACK','HARDCOVER','EBOOK','AUDIOBOOK')),
    price             NUMERIC(10,2) NOT NULL,
    currency          CHAR(3) NOT NULL DEFAULT 'INR',
    estimated_delivery VARCHAR(100),
    rating            NUMERIC(3,2) NOT NULL DEFAULT 0,
    description       TEXT,
    publisher_name    VARCHAR(255),
    published_date    DATE,
    language          VARCHAR(50) DEFAULT 'English',
    isbn              VARCHAR(20),
    total_sales       INT NOT NULL DEFAULT 0,
    total_reviews     INT NOT NULL DEFAULT 0,
    is_new_launch     BOOLEAN NOT NULL DEFAULT FALSE,
    is_bestseller     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_books_author ON books(author_id);
CREATE INDEX idx_books_bestseller ON books(is_bestseller);
CREATE INDEX idx_books_new_launch ON books(is_new_launch);

-- Book categories
CREATE TABLE book_categories (
    book_id  UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    PRIMARY KEY (book_id, category)
);

-- Reviews
CREATE TABLE reviews (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id    UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating     SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    body       TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (book_id, user_id)
);

CREATE INDEX idx_reviews_book ON reviews(book_id);

-- Addresses
CREATE TABLE addresses (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city          VARCHAR(100) NOT NULL,
    state         VARCHAR(100) NOT NULL,
    pin_code      VARCHAR(20) NOT NULL,
    country       VARCHAR(100) NOT NULL DEFAULT 'India',
    email         VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(20) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_addresses_user ON addresses(user_id);

-- Carts
CREATE TABLE carts (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    currency   CHAR(3) NOT NULL DEFAULT 'INR',
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Cart items
CREATE TABLE cart_items (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id    UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    book_id    UUID NOT NULL REFERENCES books(id),
    quantity   INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price NUMERIC(10,2) NOT NULL,
    UNIQUE (cart_id, book_id)
);

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);

-- Wishlists
CREATE TABLE wishlists (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Wishlist books
CREATE TABLE wishlist_books (
    wishlist_id UUID NOT NULL REFERENCES wishlists(id) ON DELETE CASCADE,
    book_id     UUID NOT NULL REFERENCES books(id),
    PRIMARY KEY (wishlist_id, book_id)
);

-- Orders
CREATE TABLE orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id),
    delivery_address_id UUID REFERENCES addresses(id),
    subtotal            NUMERIC(10,2) NOT NULL,
    tax                 NUMERIC(10,2) NOT NULL DEFAULT 0,
    delivery_charges    NUMERIC(10,2) NOT NULL DEFAULT 0,
    coupon_code         VARCHAR(50),
    discount            NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_payable       NUMERIC(10,2) NOT NULL,
    currency            CHAR(3) NOT NULL DEFAULT 'INR',
    status              VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'
                        CHECK (status IN ('CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED')),
    estimated_delivery  VARCHAR(100),
    placed_at           TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_user ON orders(user_id);

-- Order items
CREATE TABLE order_items (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id   UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    book_id    UUID NOT NULL REFERENCES books(id),
    quantity   INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_order_items_order ON order_items(order_id);

-- Payment transactions
CREATE TABLE payment_transactions (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id       UUID NOT NULL UNIQUE REFERENCES orders(id),
    payment_method VARCHAR(20) NOT NULL
                   CHECK (payment_method IN ('CREDIT_CARD','DEBIT_CARD','UPI','WALLET')),
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                   CHECK (status IN ('PENDING','SUCCESS','FAILED','CANCELLED')),
    payable_amount NUMERIC(10,2) NOT NULL,
    currency       CHAR(3) NOT NULL DEFAULT 'INR',
    paid_at        TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);
