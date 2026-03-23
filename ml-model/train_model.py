import pandas as pd
import pickle

from sklearn.model_selection import train_test_split
from sklearn.neighbors import KNeighborsClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import accuracy_score, classification_report

# load dataset
df = pd.read_csv("dataset/financial_health_dataset.csv")

X = df[["savings_rate","expense_ratio","debt_ratio","investment_ratio"]]
y = df["health"]

# split dataset
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# pipeline
model = Pipeline([
    ("scaler", StandardScaler()),
    ("knn", KNeighborsClassifier(n_neighbors=5))
])

# train model
model.fit(X_train, y_train)

# predictions
y_pred = model.predict(X_test)

# accuracy
accuracy = accuracy_score(y_test, y_pred)

print("Model Accuracy:", accuracy)

print("\nClassification Report:")
print(classification_report(y_test, y_pred))

# save model
pickle.dump(model, open("finance_model.pkl","wb"))

print("\nModel saved as finance_model.pkl")